import os
import scrubadub
import time
from email.parser import Parser
from email_reply_parser import EmailReplyParser
from difflib import Differ
import urllib

path='/data path/'
userfolder='folder path'
#api key for miscrosoft conceptual graph     
apiKey = 'UYHYOz50DBBtVLJjeCHICIEIVJfbJFZG'

name_relate=['name','city','job','male','female','person','man','woman','author','composer','player','artist']

#remove double space and multiple lines and add space behind a line	
def normalize(string):
	t=string.split(" ")
	d1=[]
	for x in t:
		if x:
			d1.append(x)
	ret=' '.join(d1)
	d2=[]	
	for x in ret.split("\n"):
		if x:
			d2.append(x)
	ret='\n '.join(d2)
	return ret
#find the differences between two string
def find_differences(s1, s2):
	l1 = s1.split(' ')
	l2 = s2.split(' ')
	dif = list(Differ().compare(l1, l2))
	result={}
	origin=[]
	modified=[]
	for i in dif:
		if i[:1] == '-':
			if i[2:].strip().encode('utf8').isdigit() and len(origin)!=0 and origin[len(origin)-1].replace(' ',"").isdigit():
				origin[len(origin)-1]=origin[len(origin)-1]+' '+i[2:].strip().encode('utf8')
			else:
				origin.append(i[2:].strip().encode('utf8'))
		elif i[:1] == '+':
			modified.append (i[2:].strip().encode('utf8'))
	while len(origin)!=0 and len(modified)!=0 and len(origin)==len(modified):
		key=origin.pop()
		t_type=filter(lambda ch: ch not in ",?!./;:()", modified.pop())
		if t_type=='{{NAME}}':
			key=filter(lambda ch: ch not in ",?!./;:()", key)
		result[key]=t_type
	return result

start_time = time.time()

size=100
path+=userfolder

for count,filename in enumerate(os.listdir(path)):
	if count>=size:break
	print "\n"+str(count)+"  file name ",userfolder+filename
	data=open(path+filename,'r').read()
	
	email=Parser().parsestr(data)
	body=email.get_payload().decode('utf8').strip()
	body=normalize(body)
	reply=EmailReplyParser.parse_reply(body)

	#filter out the forwared email
	if reply.find("-")!= -1 and reply.find("Forwarded by")!= -1:continue
		
	cleaned=scrubadub.clean(reply)

	difference_table = find_differences(reply,cleaned)

	for key in difference_table.keys():
		if not key: break
		if difference_table[key]=='{{NAME}}':
			url = "https://concept.research.microsoft.com/api/Concept/ScoreByProb?instance="+key+"&topK=10&smooth=0&api_key="+apiKey
			response= urllib.urlopen(url)
			t_dict=eval(response.read())
			flag=0
			if not t_dict:flag=1
			else:
				for t_key in t_dict.keys():
					if t_key in name_relate:
						flag=1
						break
			if flag==1:
				difference_table[key]=1

	for key in difference_table.keys():
		if difference_table[key]==1:
			reply=reply.replace(key,'{{NAME}}')
		elif difference_table[key]!='{{NAME}}':
			reply=reply.replace(key,difference_table[key])

print("--- %s seconds ---" % (time.time() - start_time))